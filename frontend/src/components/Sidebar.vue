<template>
  <!-- sidebar -->
  <nav class="sidebar bg-white dark:bg-gray-800 overflow-y-auto h-screen">
    <!-- sidebar head -->
    <div class="sidebar-head p-4">
      <router-link
        to="/"
        exact
        class="flex"
      >
        <img
          class="w-8 mt-1 flex-shrink-0"
          src="@/assets/logo/logo.svg"
          alt="logo windzo"
          style="object-fit: contain; height: 32px; width: 32px;"
        />
        <h2
          class="text-2xl font-normal ml-3 mt-2 text-gray-800 dark:text-gray-200"
          translate="no"
        >
          Windzo<span class="text-primary">.</span>
        </h2>
      </router-link>
      <div
        class="bg-gray-700 absolute mt-3 dark:block hidden rounded-md py-1 px-2 text-xs text-gray-200"
      >
        {{ $t('sidebar.darkMode') }}
      </div>
      <button
        class="lg:hidden block dark:text-gray-400 float-right -mt-7"
        @click="$emit('sidebarToggle')"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
          role="img"
          width="25px"
          height="25px"
          preserveAspectRatio="xMidYMid meet"
          viewBox="0 0 32 32"
        >
          <path
            fill="currentColor"
            d="M7.219 5.781L5.78 7.22L14.563 16L5.78 24.781l1.44 1.439L16 17.437l8.781 8.782l1.438-1.438L17.437 16l8.782-8.781L24.78 5.78L16 14.563z"
          />
        </svg>
      </button>
    </div>
    <!-- sidebar list -->
    <div class="sidebar-list p-4 mt-4 divide-y dark:divide-gray-700">
      <div class="pb-5">
        <p class="font-medium text-gray-400 dark:text-gray-400">{{ $t('sidebar.menu') }}</p>
        <div class="wrap-item mt-4 dark:text-gray-500">
          <div class="item">
            <router-link
              to="/dashboard"
              exact
              class="w-full flex text-left rounded-md box-border p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
            >
              <span class="mr-3 text-xl"><Icon icon="bxs:dashboard" /></span>
              <span class="w-full"> {{ $t('dashboard.title') }} </span>
            </router-link>
          </div>
          <div class="item mt-3">
            <menu-accordion>
              <template v-slot:icon>
                <Icon icon="mdi:office-building" />
              </template>
              <template v-slot:title> {{ $t('sidebar.tenant') }} </template>
              <template v-slot:content>
                <router-link
                  to="/tenant/overview"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.overview') }}
                </router-link>
                <router-link
                  to="/tenant/members"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.member') }}
                </router-link>
                <router-link
                  to="/tenant-gateway"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.switchTenant') }}
                </router-link>
                <!-- Temporarily hidden
                <router-link
                  to="/tenant/settings"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  Settings
                </router-link>
                -->
              </template>
            </menu-accordion>
          </div>
          <div class="item mt-3">
            <menu-accordion>
              <template v-slot:icon>
                <Icon icon="mdi:robot" />
              </template>
              <template v-slot:title> {{ $t('sidebar.pennyBots') }} </template>
              <template v-slot:content>
                <router-link
                  to="/penny-bots"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.bots') }}
                </router-link>
                <router-link
                  to="/penny-connections"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.connections') }}
                </router-link>
                <router-link
                  to="/penny-rules"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.rules') }}
                </router-link>
              </template>
            </menu-accordion>
          </div>
          <div class="item mt-3">
            <menu-accordion>
              <template v-slot:icon>
                <Icon icon="mdi:message-text" />
              </template>
              <template v-slot:title> {{ $t('sidebar.messages') }} </template>
              <template v-slot:content>
                <router-link
                  to="/messages"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.conversations') }}
                </router-link>
              </template>
            </menu-accordion>
          </div>
          <div class="item mt-3">
            <menu-accordion>
              <template v-slot:icon>
                <Icon icon="mdi:account-multiple" />
              </template>
              <template v-slot:title> {{ $t('customers.title') }} </template>
              <template v-slot:content>
                <router-link
                  to="/customers"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('customers.title') }}
                </router-link>
              </template>
            </menu-accordion>
          </div>
          <div class="item mt-3" v-if="tenantStore?.currentTenant?.role === 'OWNER'">
            <menu-accordion>
              <template v-slot:icon>
                <Icon icon="mdi:bank-transfer" />
              </template>
              <template v-slot:title> {{ $t('sidebar.payments') }} </template>
              <template v-slot:content>
                <router-link
                  to="/payment/deposit"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.deposit') }}
                </router-link>
                <router-link
                  to="/payment/history"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.history') }}
                </router-link>
              </template>
            </menu-accordion>
          </div>
          <div class="item mt-3" v-if="authStore.isAnyAdmin">
            <menu-accordion>
              <template v-slot:icon>
                <Icon icon="mdi:shield-account" />
              </template>
              <template v-slot:title> {{ $t('sidebar.admin') }} </template>
              <template v-slot:content>
                <router-link
                  to="/admin/bank-account"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.bankAccount') }}
                </router-link>
                <router-link
                  to="/admin/packages"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.packages') }}
                </router-link>
                <router-link
                  to="/admin/discounts"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.discounts') }}
                </router-link>
                <router-link
                  to="/admin/analytics"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.analytics') }}
                </router-link>
                <router-link
                  to="/admin/webhooks"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.webhooks') }}
                </router-link>
                <router-link
                  to="/admin/users"
                  @click.stop
                  class="w-full text-left block rounded-md p-3 hover:bg-gray-200 dark:hover:bg-gray-700"
                >
                  {{ $t('sidebar.userManagement') }}
                </router-link>
              </template>
            </menu-accordion>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>
<script>
  import { Icon } from "@iconify/vue";
  import MenuAccordion from "./MenuAccordion.vue";
  import { useAuthStore } from "@/stores/authStore";
  import { useGatewayTenantStore } from "@/stores/tenant/gateway/myTenantStore";

  export default {
    components: {
      Icon,
      MenuAccordion,
    },
    setup() {
      const authStore = useAuthStore();
      const tenantStore = useGatewayTenantStore();
      return {
        authStore,
        tenantStore,
      };
    },
  };
</script>
