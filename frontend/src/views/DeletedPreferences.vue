<template>
  <v-container>
    <v-card class="mb-4">
      <v-card-title>
        已刪除清單
        <v-spacer></v-spacer>
      </v-card-title>

      <v-data-table
        :headers="headers"
        :items="preferences"
        :loading="loading"
        class="elevation-1"
      >
        <template v-slot:item.price="{ item }">
          {{ formatCurrency(item.price) }}
        </template>
        <template v-slot:item.totalFee="{ item }">
          {{ formatCurrency(item.totalFee) }}
        </template>
        <template v-slot:item.totalAmount="{ item }">
          {{ formatCurrency(item.totalAmount) }}
        </template>
        <template v-slot:item.action="{ item }">
          <v-btn class="mr-2 esun-green-btn" size="small" @click="restorePreference(item.sn)">
            復原
          </v-btn>
          <v-btn class="esun-green-btn" size="small" @click="hardDeletePreference(item.sn)">
            永久刪除
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import api from '../utils/api'

const headers = [
  { title: '使用者 ID', key: 'userId' },
  { title: '金融商品', key: 'productName' },
  { title: '商品價格', key: 'price' },
  { title: '購買數量', key: 'purchaseQuantity' },
  { title: '手續費用', key: 'totalFee' },
  { title: '預計扣款總金額', key: 'totalAmount' },
  { title: '扣款帳號', key: 'account' },
  { title: '聯絡信箱', key: 'email' },
  { title: '操作', key: 'action', sortable: false }
]

const preferences = ref([])
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const resAcc = await api.get('/users/active')
    let users = []
    if (resAcc.success) users = resAcc.data

    const resPref = await api.get('/preferences/deleted')
    if (resPref.success) {
      preferences.value = resPref.data.map(pref => {
        const found = users.find(u => u.userId === pref.userId)
        return {
          ...pref,
          email: found ? found.email : '未知'
        }
      })
    }
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

const restorePreference = async (sn) => {
  const result = await Swal.fire({
    title: '確認復原',
    text: '確定要復原此喜好清單嗎？',
    icon: 'question',
    showCancelButton: true,
    customClass: {
      confirmButton: 'esun-green-btn',
      cancelButton: 'esun-cancel-btn'
    },
    confirmButtonText: '確定',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    try {
      const res = await api.patch(`/preferences/${sn}/restore`)
      if (res.success) {
        showSuccess('已復原喜好清單')
        fetchData()
      } else {
        showError(res.message)
      }
    } catch (error) {
      showError(error)
    }
  }
}

const hardDeletePreference = async (sn) => {
  const result = await Swal.fire({
    title: '永久刪除警告',
    text: '您確定要永久刪除這筆資料嗎？刪除後無法進行復原！',
    icon: 'warning',
    showCancelButton: true,
    customClass: {
      confirmButton: 'esun-green-btn',
      cancelButton: 'esun-cancel-btn'
    },
    confirmButtonText: '確定永久刪除',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    try {
      const res = await api.delete(`/preferences/${sn}/hard`)
      if (res.success) {
        showSuccess('已永久刪除喜好清單')
        fetchData()
      } else {
        showError(res.message)
      }
    } catch (error) {
      showError(error)
    }
  }
}

const showSuccess = (text) => {
  Swal.fire({
    icon: 'success',
    title: '成功',
    text: text,
    timer: 1500,
    showConfirmButton: false
  })
}

const showError = (text) => {
  const errMsg = text.response?.data?.message || text.message || String(text)
  Swal.fire({
    icon: 'error',
    title: '錯誤',
    text: errMsg
  })
}

const formatCurrency = (value) => {
  if (value == null || isNaN(value)) return ''
  return `$${Number(value).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 4 })}`
}

onMounted(() => {
  fetchData()
})
</script>
