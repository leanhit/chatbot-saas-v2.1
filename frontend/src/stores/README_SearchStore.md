# SearchStore - Global Search Management

## Overview
`useSearchStore` provides centralized search functionality across the entire application. It manages search queries, history, filters, and context for different components.

## Features
- ✅ Global search state management
- ✅ Search history with autocomplete
- ✅ Context-aware filtering
- ✅ Persistent search across navigation
- ✅ Reusable search components
- ✅ **Navbar integration** - Centralized search in main header

## Navbar Integration

The search functionality is now integrated into the main application navbar (`/components/Header.vue`):

### Features in Navbar:
- ✅ **Global search input** - Replaces individual search bars
- ✅ **Context-aware placeholder** - Changes based on current page context
- ✅ **Clear button** - Appears when search is active
- ✅ **Context indicator** - Shows current search context
- ✅ **Responsive design** - Hidden on mobile, visible on desktop

### Context-Aware Placeholders:
```javascript
// Automatically changes based on current context:
'members' → 'Search members...'
'projects' → 'Search projects...'
'files' → 'Search files...'
'default' → 'Search...'
```

### Visual Indicators:
- **Context Badge**: Shows "Searching in Members" when active
- **Clear Button**: X button to clear search
- **Search Icon**: Visual search indicator

## Basic Usage

### 1. Set Search Context in Your Component
```javascript
import { useSearchStore } from '@/stores/searchStore'
import { onMounted } from 'vue'

setup() {
  const searchStore = useSearchStore()
  
  // Set search context when component mounts
  onMounted(() => {
    searchStore.setSearchContext('members') // or 'projects', 'files', etc.
  })
}
```

### 2. Use Search State in Your Component
```javascript
// Access search query from navbar
const searchQuery = computed(() => searchStore.searchQuery)

// Check if search is active
const hasSearch = computed(() => searchStore.hasQuery)

// Filter your data based on navbar search
const filteredItems = computed(() => {
  if (searchStore.hasQuery) {
    const query = searchStore.searchQuery.toLowerCase()
    return items.value.filter(item => 
      item.name.toLowerCase().includes(query)
    )
  }
  return items.value
})
```

### 3. No Need for Local Search Components
```vue
<!-- BEFORE: Local search bar -->
<template>
  <div>
    <GlobalSearchBar placeholder="Search..." />
    <MemberList :members="filteredMembers" />
  </div>
</template>

<!-- AFTER: Use navbar search -->
<template>
  <div>
    <!-- No search bar needed - navbar handles it -->
    <MemberList :members="filteredMembers" />
  </div>
</template>
```

## Migration from Local Search

### Step 1: Remove Local Search Components
```vue
<!-- Remove these from your components -->
<GlobalSearchBar />
<input v-model="localSearchQuery" />
```

### Step 2: Add Search Context
```javascript
// Add to your component setup
onMounted(() => {
  searchStore.setSearchContext('your-context')
})
```

### Step 3: Use Global Search State
```javascript
// Replace local search with global
const filteredData = computed(() => {
  if (searchStore.hasQuery) {
    // Use searchStore.searchQuery instead of local
    return data.filter(item => 
      item.name.includes(searchStore.searchQuery)
    )
  }
  return data
})
```

## API Reference

### State
- `searchQuery: string` - Current search query (from navbar)
- `searchContext: string` - Current search context ('members', 'projects', etc.)
- `isSearchActive: boolean` - Whether search is currently active
- `searchHistory: string[]` - Search history (max 10 items)
- `filters: object` - Context-specific filters

### Computed
- `hasQuery: boolean` - Whether search query has content
- `isEmpty: boolean` - Whether search is empty

### Actions

#### Search Management
```javascript
searchStore.setSearchQuery('query')     // Set search query (navbar handles this)
searchStore.clearSearch()               // Clear search query
searchStore.resetSearch()               // Reset all search state
```

#### Context Management
```javascript
searchStore.setSearchContext('projects') // Set search context
```

#### Filter Management
```javascript
searchStore.setFilter('status', 'active')  // Set filter value
searchStore.getFilter('status')            // Get filter value
searchStore.clearFilters()                 // Clear all filters
```

#### History Management
```javascript
searchStore.removeFromHistory('query')  // Remove from history
searchStore.clearHistory()              // Clear all history
```

## Component Examples

### Member Management (Using Navbar Search)
```vue
<template>
  <div>
    <!-- No search bar needed - navbar handles search -->
    <div class="filters">
      <select @change="searchStore.setFilter('role', $event.target.value)">
        <option value="">All Roles</option>
        <option value="admin">Admin</option>
        <option value="member">Member</option>
      </select>
    </div>
    <MemberList :members="filteredMembers" />
  </div>
</template>

<script>
import { computed, onMounted } from 'vue'
import { useSearchStore } from '@/stores/searchStore'

export default {
  setup() {
    const searchStore = useSearchStore()
    const members = ref([])
    
    // Set context for navbar placeholder
    onMounted(() => {
      searchStore.setSearchContext('members')
    })
    
    // Filter using navbar search query
    const filteredMembers = computed(() => {
      let filtered = members.value
      
      // Apply navbar search
      if (searchStore.hasQuery) {
        const query = searchStore.searchQuery.toLowerCase()
        filtered = filtered.filter(member => 
          member.name.toLowerCase().includes(query)
        )
      }
      
      // Apply local filters
      const roleFilter = searchStore.getFilter('role')
      if (roleFilter) {
        filtered = filtered.filter(member => member.role === roleFilter)
      }
      
      return filtered
    })
    
    return { filteredMembers }
  }
}
</script>
```

### Project Management
```vue
<template>
  <div>
    <ProjectGrid :projects="filteredProjects" />
  </div>
</template>

<script>
export default {
  setup() {
    const searchStore = useSearchStore()
    const projects = ref([])
    
    onMounted(() => {
      searchStore.setSearchContext('projects')
    })
    
    const filteredProjects = computed(() => {
      if (searchStore.hasQuery) {
        return projects.value.filter(project => 
          project.name.toLowerCase().includes(searchStore.searchQuery.toLowerCase())
        )
      }
      return projects.value
    })
    
    return { filteredProjects }
  }
}
</script>
```

## Benefits of Navbar Integration

### 1. **Consistent UX**
- Single search experience across entire app
- Same search behavior in all components
- Consistent visual design

### 2. **Space Efficiency**
- No duplicate search bars in components
- More screen real estate for content
- Cleaner component layouts

### 3. **Global Features**
- Search history works across all contexts
- Single clear button for all searches
- Context indicators help users understand scope

### 4. **Performance**
- Single search state reduces memory usage
- No duplicate search logic
- Centralized search management

## Troubleshooting

### Search Not Working in Component
- Ensure `searchStore.setSearchContext()` is called in `onMounted()`
- Check that component uses `searchStore.searchQuery` (reactive)
- Verify navbar is properly integrated with searchStore

### Placeholder Not Updating
- Check that context is set correctly: `searchStore.setSearchContext('members')`
- Verify context cases match the switch statement in Header.vue
- Ensure computed property is reactive

### Context Indicator Not Showing
- Verify search is active: `searchStore.hasQuery`
- Check that context is set: `searchStore.searchContext`
- Ensure SearchContextIndicator component is imported in Header.vue

## Best Practices

### 1. Always Set Context
```javascript
onMounted(() => {
  searchStore.setSearchContext('your-context')
})
```

### 2. Use Computed Properties
```javascript
const filteredData = computed(() => {
  // Reactively filter based on navbar search
})
```

### 3. Clean Up on Unmount (Optional)
```javascript
onUnmounted(() => {
  // Optionally reset search when leaving sensitive pages
  // searchStore.resetSearch()
})
```

### 4. Provide Clear Context Names
Use clear, simple context names:
- ✅ 'members', 'projects', 'files'
- ❌ 'member-management-panel', 'project-overview-page'

## Future Enhancements

- **Search Suggestions**: Add autocomplete dropdown in navbar
- **Advanced Filters**: Expand filter options in navbar
- **Search Shortcuts**: Add keyboard shortcuts (Ctrl+K)
- **Search Analytics**: Track search usage patterns
- **Global Search Results**: Show results from multiple contexts
