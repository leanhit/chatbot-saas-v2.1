<template>
  <div class="container lg:max-w-7xl mx-auto text-center mt-40">
    <div class="space-y-8">
      <h1 class="text-9xl dark:text-white text-gray-800 font-bold">404</h1>
      <h2 class="text-4xl dark:text-white text-gray-800">
        {{ $t('error.title') }}
      </h2>
      <p class="dark:text-gray-500 text-gray-800">
        {{ $t('error.subtitle') }}
        {{ this.$route.params.pathMatch[0] }} {{ $t('error.notFound') }}!
      </p>
      <button
        @click="this.$router.push('/')"
        class="bg-primary rounded text-white py-3 px-5"
      >
        {{ $t('error.returnToDashboard') }}
      </button>
    </div>
  </div>
</template>
