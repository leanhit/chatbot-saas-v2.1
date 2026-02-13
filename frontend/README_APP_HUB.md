# 📱 Frontend App Hub Management

## 🎯 Overview

Frontend App Hub Management cung cấp giao diện người dùng đầy đủ để quản lý các ứng dụng trong hệ thống Chatbot SaaS. Các components được xây dựng với Vue 3, Composition API, và Tailwind CSS.

## 📁 File Structure

```
frontend/src/
├── views/app-hub/
│   ├── Dashboard.vue          # App Hub Dashboard
│   ├── AppRegistry.vue        # App Registry (list, search, filter)
│   └── CreateApp.vue          # Create App Wizard
├── api/
│   └── appApi.js              # API integration layer
├── stores/
│   └── appStore.js            # Pinia state management
└── components/
    └── Sidebar.vue             # Updated with App Hub menu
```

## 🚀 Features

### 1. App Hub Dashboard (`/app-hub`)
- **Statistics Cards**: Total apps, active apps, subscriptions, public apps
- **Recent Apps**: Grid view of recently created apps
- **Quick Actions**: Create app, browse marketplace, manage subscriptions

### 2. App Registry (`/app-hub/apps`)
- **Advanced Search**: Search by name, description
- **Multi-filter**: Type, status, visibility filters
- **Sortable Table**: Sort by any column
- **Bulk Actions**: Activate/deactivate/delete multiple apps
- **Pagination**: Efficient data loading
- **Export**: Export apps to JSON

### 3. Create App Wizard (`/app-hub/apps/create`)
- **3-Step Process**: Basic Info → Configuration → Review
- **Form Validation**: Real-time validation with error messages
- **JSON Editor**: Schema and default configuration with validation
- **Progress Indicator**: Visual progress bar

## 🔧 Technical Implementation

### API Integration (`appApi.js`)
```javascript
// App Management
appApi.getApps(params)
appApi.createApp(data)
appApi.updateApp(id, data)
appApi.deleteApp(id)

// Subscription Management
appApi.getSubscriptions(params)
appApi.subscribeToApp(data)
appApi.cancelSubscription(id)

// Search & Filter
appApi.searchApps(params)
appApi.getPublicApps(params)
```

### State Management (`appStore.js`)
```javascript
// Reactive state
apps: []
currentApp: null
subscriptions: []
loading: false
error: null

// Computed getters
activeApps: computed(() => apps.filter(app => app.status === 'ACTIVE'))
publicAppsOnly: computed(() => apps.filter(app => app.isPublic))

// Actions
fetchApps(params)
createApp(data)
bulkUpdateAppStatus(ids, status)
```

### Component Architecture
- **Composition API**: Modern Vue 3 syntax
- **Reactive Data**: `ref()` and `reactive()`
- **Computed Properties**: Efficient derived state
- **Lifecycle Hooks**: `onMounted()`
- **Event Handling**: Form validation, API calls

## 🎨 UI/UX Features

### Responsive Design
- **Mobile-first**: Optimized for all screen sizes
- **Touch-friendly**: Large tap targets
- **Adaptive Layout**: Grid systems that adapt

### Interactive Elements
- **Hover States**: Visual feedback
- **Loading States**: Spinners and disabled buttons
- **Error Handling**: User-friendly error messages
- **Success Feedback**: Toast notifications

### Accessibility
- **Semantic HTML**: Proper heading hierarchy
- **ARIA Labels**: Screen reader support
- **Keyboard Navigation**: Tab order and focus management
- **Color Contrast**: WCAG compliant colors

## 🔗 Navigation

### Router Configuration
```javascript
// App Hub Routes
{
  path: "/app-hub",
  component: AppHubDashboard
},
{
  path: "/app-hub/apps", 
  component: AppRegistry
},
{
  path: "/app-hub/apps/create",
  component: CreateApp
}
```

### Sidebar Menu
```vue
<menu-accordion>
  <template v-slot:icon>
    <Icon icon="mdi:application" />
  </template>
  <template v-slot:title> App Hub </template>
  <template v-slot:content>
    <router-link to="/app-hub">Dashboard</router-link>
    <router-link to="/app-hub/apps">App Registry</router-link>
    <router-link to="/app-hub/apps/create">Create App</router-link>
  </template>
</menu-accordion>
```

## 📊 Data Flow

### App Registry Flow
1. **Load**: `fetchApps()` → API call → Update state
2. **Filter**: Computed property filters apps array
3. **Sort**: Client-side sorting with direction toggle
4. **Paginate**: Slice array for current page
5. **Actions**: CRUD operations with optimistic updates

### Create App Flow
1. **Step 1**: Basic info validation
2. **Step 2**: JSON schema validation
3. **Step 3**: Review and confirmation
4. **Submit**: API call → Redirect to app detail

## 🛡️ Error Handling

### API Errors
```javascript
try {
  await appStore.createApp(appData)
  // Success handling
} catch (error) {
  this.error = error.response?.data?.message || error.message
  // Error display
}
```

### Form Validation
```javascript
const validateName = () => {
  if (!app.name.trim()) {
    errors.name = 'App name is required'
  } else if (!/^[a-z0-9-]+$/.test(app.name)) {
    errors.name = 'Invalid format'
  } else {
    delete errors.name
  }
}
```

## 🎯 Usage Examples

### Creating an App
```javascript
// In CreateApp.vue
const createApp = async () => {
  submitting.value = true
  try {
    const response = await appStore.createApp(appData)
    router.push(`/app-hub/apps/${response.id}`)
  } catch (error) {
    // Handle error
  } finally {
    submitting.value = false
  }
}
```

### Searching Apps
```javascript
// In AppRegistry.vue
const handleSearch = () => {
  currentPage.value = 1
  // Triggers computed property update
}

const filteredApps = computed(() => {
  let apps = [...appStore.apps]
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    apps = apps.filter(app => 
      app.name.toLowerCase().includes(query)
    )
  }
  
  return apps
})
```

## 🔄 Integration with Backend

### API Endpoints Used
- `GET /api/v1/apps` - List apps with pagination
- `POST /api/v1/apps` - Create new app
- `PUT /api/v1/apps/:id` - Update app
- `DELETE /api/v1/apps/:id` - Delete app
- `GET /api/v1/apps/search` - Search apps
- `GET /api/v1/subscriptions` - List subscriptions

### Data Mapping
```javascript
// Backend → Frontend mapping
{
  id: "uuid",
  name: "app-name", 
  displayName: "App Name",
  appType: "CHATBOT",
  status: "ACTIVE",
  isPublic: true,
  subscriptionCount: 5,
  createdAt: "2026-02-13T12:00:00Z"
}
```

## 🚀 Getting Started

### 1. Navigate to App Hub
- Click "App Hub" in sidebar
- Or go to `/app-hub`

### 2. Create Your First App
- Click "Create App" button
- Fill in basic information
- Configure endpoints (optional)
- Review and submit

### 3. Manage Apps
- Use search and filters to find apps
- Click actions to view/edit/delete
- Use bulk actions for multiple apps

## 📱 Mobile Usage

### Touch Gestures
- **Tap**: Select items, buttons
- **Swipe**: Navigate between pages
- **Long Press**: Context menus (future)

### Responsive Breakpoints
- **Mobile**: < 768px - Single column
- **Tablet**: 768px - 1024px - Two columns
- **Desktop**: > 1024px - Full layout

## 🔮 Future Enhancements

### Planned Features
- **App Detail Page**: Full app management
- **Subscription Manager**: Subscription lifecycle
- **App Marketplace**: Public app discovery
- **Analytics Dashboard**: Usage statistics
- **Bulk Import**: Import apps from CSV/JSON

### UI Improvements
- **Drag & Drop**: Reorder apps
- **Kanban View**: Visual app management
- **Real-time Updates**: WebSocket integration
- **Advanced Filters**: Date ranges, custom filters

---

**🎉 Frontend App Hub Management is now ready for use!** 

All core functionality is implemented with modern Vue 3 patterns, responsive design, and comprehensive error handling.
