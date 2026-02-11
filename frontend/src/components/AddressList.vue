<template>
  <div class="space-y-4">
    <div class="flex justify-between items-center">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">Address Book</h3>
      <button
        @click="$emit('add')"
        class="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-primary hover:bg-primary/80 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
      >
        <Icon icon="mdi:plus" class="h-4 w-4 mr-1" />
        Add Address
      </button>
    </div>
    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">Loading addresses...</p>
    </div>
    <div v-else-if="addresses.length === 0" class="text-center py-8">
      <Icon icon="mdi:map-marker-off" class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">No addresses found</p>
      <button
        @click="$emit('add')"
        class="mt-4 text-sm text-primary hover:text-primary/80"
      >
        Add your first address
      </button>
    </div>
    <div v-else class="space-y-3">
      <div
        v-for="address in addresses"
        :key="address.id"
        class="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 relative"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <h4 class="text-sm font-medium text-gray-900 dark:text-white">
                {{ address.fullAddress }}
              </h4>
              <span
                v-if="address.isDefault"
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary text-white"
              >
                Default
              </span>
            </div>
            <div class="mt-2 text-sm text-gray-600 dark:text-gray-400">
              <div v-if="address.houseNumber" class="flex items-center">
                <Icon icon="mdi:home" class="h-4 w-4 mr-1" />
                {{ address.houseNumber }}
              </div>
              <div v-if="address.street" class="flex items-center">
                <Icon icon="mdi:road" class="h-4 w-4 mr-1" />
                {{ address.street }}
              </div>
              <div v-if="address.ward" class="flex items-center">
                <Icon icon="mdi:map-marker" class="h-4 w-4 mr-1" />
                {{ address.ward }}, {{ address.district }}, {{ address.province }}
              </div>
              <div v-if="address.country" class="flex items-center">
                <Icon icon="mdi:earth" class="h-4 w-4 mr-1" />
                {{ address.country }}
              </div>
            </div>
          </div>
          <div class="flex items-center space-x-2 ml-4">
            <button
              v-if="!address.isDefault"
              @click="handleSetDefault(address.id)"
              class="text-gray-400 hover:text-primary transition-colors"
              title="Set as default"
            >
              <Icon icon="mdi:star-outline" class="h-5 w-5" />
            </button>
            <button
              @click="handleEdit(address)"
              class="text-gray-400 hover:text-blue-600 transition-colors"
              title="Edit address"
            >
              <Icon icon="mdi:pencil" class="h-5 w-5" />
            </button>
            <button
              @click="handleDelete(address.id)"
              class="text-gray-400 hover:text-red-600 transition-colors"
              title="Delete address"
            >
              <Icon icon="mdi:trash-can" class="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
export default {
  name: 'AddressList',
  components: {
    Icon
  },
  props: {
    addresses: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['add', 'edit', 'delete', 'setDefault'],
  setup(props, { emit }) {
    const handleEdit = (address) => {
      emit('edit', address)
    }
    const handleDelete = async (addressId: number) => {
      if (confirm('Are you sure you want to delete this address?')) {
        emit('delete', addressId)
      }
    }
    const handleSetDefault = async (addressId: number) => {
      emit('setDefault', addressId)
    }
    return {
      handleEdit,
      handleDelete,
      handleSetDefault
    }
  }
}
</script>
