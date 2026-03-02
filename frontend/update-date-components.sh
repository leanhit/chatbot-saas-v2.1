#!/bin/bash

# Script to update components to use dateUtils.js

echo "Updating components to use dateUtils.js..."

# Files to update
files=(
  "src/views/penny/components/PennyConnectionsModal.vue"
  "src/views/penny/components/PennyBotChatModal.vue"
  "src/views/profile/Profile.vue"
  "src/views/tenant/overview/components/tabs/BasicInfoTab.vue"
  "src/views/tenant/overview/components/AvatarCard.vue"
  "src/views/tenant/gateway/components/MyTenantTab.vue"
  "src/views/tenant/member/components/PendingMemberTab.vue"
  "src/views/tenant/member/components/ActiveMemberTab.vue"
  "src/views/tenant/member/components/InviteMemberTab.vue"
  "src/views/tenant/settings/TenantSettings.vue"
)

for file in "${files[@]}"; do
  if [ -f "$file" ]; then
    echo "Processing: $file"
    
    # Add import at the top of script section
    if grep -q "formatDate.*new Date" "$file"; then
      sed -i '/import.*vue/a\
import { formatDate } from "@/utils/dateUtils"' "$file"
      echo "  ✓ Added formatDate import"
    fi
    
    if grep -q "formatDateTime.*new Date" "$file"; then
      sed -i '/import.*vue/a\
import { formatDateTime } from "@/utils/dateUtils"' "$file"
      echo "  ✓ Added formatDateTime import"
    fi
    
    # Remove local function definitions
    sed -i '/const formatDate = (dateString) => {/,/}/d' "$file"
    sed -i '/const formatDateTime = (dateString) => {/,/}/d' "$file"
    sed -i '/const formatTime = (timestamp) => {/,/}/d' "$file"
    sed -i '/const formatLastActive = (dateString) => {/,/}/d' "$file"
    sed -i '/const isExpiringSoon = (expiresAt) => {/,/}/d' "$file"
    echo "  ✓ Removed local date functions"
    
    # Fix specific cases
    if [[ "$file" == *"TenantSettings.vue"* ]]; then
      sed -i 's/new Date(tenant.expiresAt).toISOString().slice(0, 16)/formatDateTimeLocal(tenant.expiresAt)/g' "$file"
      sed -i 's/new Date(settings.value.expiresAt).toISOString()/dateTimeLocalToIso(settings.value.expiresAt)/g' "$file"
      sed -i '/import.*vue/a\
import { formatDateTimeLocal, dateTimeLocalToIso } from "@/utils/dateUtils"' "$file"
      echo "  ✓ Fixed TenantSettings date handling"
    fi
    
    if [[ "$file" == *"PennyBotChatModal.vue"* ]]; then
      sed -i '/import.*vue/a\
import { getRelativeTime } from "@/utils/dateUtils"' "$file"
      sed -i 's/const formatTime = (timestamp) => {.*return new Date(timestamp).toLocaleTimeString.*}/formatTime(timestamp)/g' "$file"
      echo "  ✓ Fixed PennyBotChatModal time handling"
    fi
    
    if [[ "$file" == *"ActiveMemberTab.vue"* ]]; then
      sed -i '/import.*vue/a\
import { formatDate, getRelativeTime } from "@/utils/dateUtils"' "$file"
      sed -i 's/const formatLastActive = (dateString) => {.*return.*Today.*}/getRelativeTime(dateString)/g' "$file"
      echo "  ✓ Fixed ActiveMemberTab relative time"
    fi
    
    if [[ "$file" == *"InviteMemberTab.vue"* ]]; then
      sed -i '/import.*vue/a\
import { formatDate } from "@/utils/dateUtils"' "$file"
      sed -i 's/const isExpiringSoon = (expiresAt) => {.*}/isExpiringSoon(expiresAt)/g' "$file"
      echo "  ✓ Fixed InviteMemberTab expiry check"
    fi
    
    echo "✓ Updated: $file"
  fi
done

echo "Update complete!"
