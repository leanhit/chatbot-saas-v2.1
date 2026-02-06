import { createApp, defineAsyncComponent } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import 'element-plus/dist/index.css';
import './assets/main.css';

import App from './App.vue';
import router from './router';
import i18n from './i18n';
import axiosInstance from './plugins/axios';
import { useAuthStore } from '@/stores/auth';

// Các thư viện hỗ trợ
import { Waypoint } from 'vue-waypoint';
import VueLazyLoad from 'vue3-lazyload';
import TextClamp from 'vue3-text-clamp';
import SkeletonBoxWithoutLoading from '@/components/SkeletonBoxWithoutLoading.vue';
import moment from 'moment';

const initApp = async () => {
  const app = createApp(App);

  // 1. Khởi tạo Pinia đầu tiên
  const pinia = createPinia();
  app.use(pinia);

  // 2. Cài đặt các plugin
  app.use(router);
  app.use(ElementPlus);
  app.use(VueLazyLoad);
  app.use(TextClamp);
  app.use(i18n);

  // 4. Đăng ký component toàn cục
  app.component(
    'no-data',
    defineAsyncComponent({
      loader: () => import('@/components/NoData.vue'),
      loadingComponent: SkeletonBoxWithoutLoading,
    })
  );

  app.component('Waypoint', Waypoint);

  // Đăng ký toàn bộ Icons từ Element Plus
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
  }

  // 5. Custom directive
  app.directive('highlight', {
    beforeMount(el, binding) {
      try {
        if (binding?.value?.keyword) {
          el.innerHTML = el.innerHTML.replace(
            new RegExp(binding.value.keyword, 'gi'),
            (match: any) => `<span class="highlightText">${match}</span>`
          );
        }
      } catch {}
    },
  });

  // 6. Global Properties & Filters
  app.config.globalProperties.$filters = {
    prettyDate(value: any) {
      if (!value) value = new Date();
      return moment(value).calendar(null, {
        sameDay: '[Hôm nay] HH:mm:ss',
        lastDay: '[Hôm qua] HH:mm:ss',
        lastWeek: 'DD/MM/YYYY HH:mm:ss',
        sameElse: 'DD/MM/YYYY HH:mm:ss',
      });
    },
    prettyDateUnix(value: any) {
      if (!value) return '--';
      return moment.unix(value).calendar(null, {
        sameDay: '[Hôm nay] HH:mm:ss',
        lastDay: '[Hôm qua] HH:mm:ss',
        lastWeek: 'DD/MM/YYYY HH:mm:ss',
        sameElse: 'DD/MM/YYYY HH:mm:ss',
      });
    },
    durationToStr(startDate: string, endDate: string) {
      const diff = moment.duration(moment(startDate).diff(moment(endDate)));
      const milliseconds: number = diff.asMilliseconds();
      let temp = Math.floor(milliseconds / 1000);
      
      const years = Math.floor(temp / 31536000);
      if (years) return years + ' năm';
      const days = Math.floor((temp %= 31536000) / 86400);
      if (days) return days + ' ngày';
      const hours = Math.floor((temp %= 86400) / 3600);
      if (hours) return hours + ' giờ';
      const minutes = Math.floor((temp %= 3600) / 60);
      if (minutes) return minutes + ' phút';
      const seconds = temp % 60;
      if (seconds) return seconds + ' giây';
      return '';
    },
  };

  app.config.globalProperties.$router = router;
  app.config.globalProperties.axios = axiosInstance;
  app.config.globalProperties.$http = axiosInstance;
  app.provide('axios', axiosInstance);

  // 7. Mount app sau khi mọi thứ đã sẵn sàng
  console.log('[BOOTSTRAP] Vue app mounting to #vue-app');
  const appElement = document.getElementById('vue-app');
  if (!appElement) {
    throw new Error('Vue mount target #vue-app not found');
  }
  app.mount('#vue-app');
  console.log('[BOOTSTRAP] Vue app mounted successfully');
};

// Chạy hàm khởi tạo
initApp();