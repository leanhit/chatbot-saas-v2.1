import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';
import { useTenantStore } from '@/stores/tenantStore';
import { identityApi, type LoginRequest } from '@/api/identityApi';
import { extractTenantIdsFromToken } from '@/utils/tokenUtils';

export default {
    props: ['viewSettings'],
    emits: ['onChangeView'],
    setup() {
        const { t } = useI18n();
        const router = useRouter();
        const authStore = useAuthStore();
        const tenantStore = useTenantStore();

        // Data bindings
        const emailLogin = ref('');
        const passwordLogin = ref('');
        const passwordConfirm = ref('');
        const isLoginView = ref(true);

        // UI state
        const emptyFields = ref(false);
        const showPassword = ref(false);
        const showPasswordConfirm = ref(false);

        function toggleView() {
            isLoginView.value = !isLoginView.value;
            emailLogin.value = '';
            passwordLogin.value = '';
            passwordConfirm.value = '';
            emptyFields.value = false;
        }

        function isValidEmail(email: string): boolean {
            const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return regex.test(email);
        }

        async function doLogin() {
            if (!emailLogin.value || !passwordLogin.value) {
                ElMessage.error(t('Please fill in all fields.'));
                emptyFields.value = true;
                return;
            }

            try {
                // 1. Call Identity Hub Login API
                const res = await identityApi.login({
                    email: emailLogin.value,
                    password: passwordLogin.value,
                } as LoginRequest);

                // Identity Hub returns LoginResponse with tokens
                const authData = res.data;

                if (!authData.accessToken) {
                    throw new Error("No access token received from Identity Hub");
                }

                // 2. Store tokens in authStore
                await authStore.login(authData);

                // 3. Extract tenant_ids from JWT instead of API call
                const tenantIds = extractTenantIdsFromToken(authData.accessToken);
                console.log('Extracted tenant_ids from JWT:', tenantIds);

                // 4. Update tenant store with extracted tenant IDs
                if (tenantIds.length > 0) {
                    // Set first tenant as current tenant (existing logic)
                    tenantStore.setCurrentTenantById(tenantIds[0]);
                }

                // 5. Route based on tenant availability (existing logic)
                if (tenantIds.length === 0) {
                    // No tenant memberships, redirect to tenant gateway
                    await router.push({ name: 'tenant-gateway' });
                } else {
                    // Has tenant memberships, go to home
                    await router.push('/');
                }

                ElMessage.success(t('Welcome back!'));
            } catch (error: any) {
                console.error("Login Component Error:", error);

                const msg = error.response?.data?.message || error.message || 'Login failed';
                ElMessage.error(t(msg));
            }
        }

        async function doRegister() {
            // Keep existing registration logic - no changes
            if (!emailLogin.value || !passwordLogin.value || !passwordConfirm.value) {
                ElMessage.error(t('Please fill in all fields.'));
                return;
            }

            if (passwordLogin.value !== passwordConfirm.value) {
                ElMessage.error(t('Passwords do not match.'));
                return;
            }

            try {
                // Still using existing registration API for now
                const { usersApi } = await import('@/api/usersApi');
                const res = await usersApi.register({
                    email: emailLogin.value,
                    password: passwordLogin.value,
                });

                // Backend returns UserResponse with token and user
                const authData = res.data;
                
                if (authData.token) {
                    // Auto-login after successful registration
                    await authStore.login(authData);
                    
                    try {
                        await tenantStore.fetchUserTenants();
                    } catch (tenantErr) {
                        console.error("Lỗi lấy Tenant sau khi register:", tenantErr);
                    }

                    if (!tenantStore.currentTenant) {
                        await router.push({ name: 'tenant-gateway' });
                    } else {
                        await router.push('/');
                    }
                    
                    ElMessage.success(t('Registration successful! Welcome!'));
                } else {
                    ElMessage.success(t('Registered successfully. Please log in.'));
                    toggleView();
                }
            } catch (err: any) {
                const msg = err.response?.data?.message || 'Registration failed';
                ElMessage.error(t(msg));
            }
        }

        return {
            t, emailLogin, passwordLogin, passwordConfirm,
            emptyFields, isLoginView, showPassword, showPasswordConfirm,
            toggleView, doLogin, doRegister,
            toggleShowPassword: () => showPassword.value = !showPassword.value,
            toggleShowPasswordConfirm: () => showPasswordConfirm.value = !showPasswordConfirm.value,
        };
    },
};