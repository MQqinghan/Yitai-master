// src/utils/categoryHelper.js
export function formatCategoryId(categoryIdList) {
  if (Array.isArray(categoryIdList) && categoryIdList.length === 3) {
    return categoryIdList[2]
  }
  return ''
}