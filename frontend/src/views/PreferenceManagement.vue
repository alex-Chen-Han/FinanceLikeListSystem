<template>
  <v-container>
    <v-card class="mb-4">
      <v-card-title class="d-flex align-center">
        喜好清單管理
        <span class="text-sub ms-4 mb-0" style="font-size: 14px; font-weight: normal; color: #666666; margin: 15px; display: inline-block; transform: translateY(6px);">
          ※ 計算說明：總金額 = ⌊(單價 × 數量) + 手續費⌋。系統將針對最終試算結果進行<strong style="color: var(--esun-primary-green);">無條件捨去</strong>至整數。
        </span>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="openDialog()">新增喜好清單</v-btn>
      </v-card-title>
      
      <v-data-table
        :headers="headers"
        :items="preferences"
        :loading="loading"
        class="elevation-1"
      >
        <template v-slot:item.deleteAction="{ item }">
          <v-btn icon variant="text" color="red" @click="deletePreference(item.sn)">
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </template>
        <template v-slot:item.price="{ item }">
          {{ formatCurrency(item.price) }}
        </template>
        <template v-slot:item.totalFee="{ item }">
          {{ formatCurrency(item.totalFee) }}
        </template>
        <template v-slot:item.totalAmount="{ item }">
          {{ formatCurrency(item.totalAmount) }}
        </template>
        <template v-slot:item.editAction="{ item }">
          <v-btn icon size="small" variant="text" color="#1eacad" @click="openDialog(item)">
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="500px">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ isEdit ? '編輯喜好清單' : '新增喜好清單' }}</span>
        </v-card-title>

        <v-card-text>
          <v-form ref="form" v-model="valid" lazy-validation>
            <v-text-field
              v-model="editedItem.userId"
              label="使用者 ID (選擇帳號後自動帶入)"
              disabled
              required
            ></v-text-field>

            <v-select
              v-model="editedItem.account"
              :items="accountOptions"
              item-title="account"
              item-value="account"
              label="預計扣款帳號"
              @update:modelValue="onAccountSelected"
              :rules="[v => !!v || '請選擇扣款帳號']"
              required
            ></v-select>

            <v-text-field
              v-model="editedItem.productName"
              label="金融商品名稱"
              :rules="[v => !!v || '請輸入金融商品名稱']"
              required
            ></v-text-field>

            <v-text-field
              v-model.number="editedItem.price"
              label="產品價格"
              type="number"
              :rules="[v => (v !== null && v !== '') || '請輸入價格', v => v >= 0 || '價格不可為負數']"
              required
            ></v-text-field>

            <v-text-field
              v-model.number="editedItem.feeRate"
              label="手續費率 (如 0.05 代表 5%)"
              type="number"
              step="0.01"
              :rules="[
                v => (v !== null && v !== '') || '請輸入手續費率', 
                v => v >= 0 || '手續費率不可為負數',
                v => v <= 1 || '手續費率不可超過 100% (即 1.0)'
              ]"
  required
            ></v-text-field>

            <v-text-field
              v-model.number="editedItem.purchaseQuantity"
              label="購買數量"
              type="number"
              :rules="[v => v > 0 || '數量不能少於 1']"
              required
            ></v-text-field>

          </v-form>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="save" :disabled="!valid">儲存</v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="closeDialog">取消</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import api from '../utils/api'

const headers = [
  { title: '', key: 'deleteAction', sortable: false, width: '60px' },
  { title: '使用者 ID', key: 'userId' },
  { title: '金融商品', key: 'productName' },
  { title: '商品價格', key: 'price' },
  { title: '購買數量', key: 'purchaseQuantity' },
  { title: '手續費用', key: 'totalFee' },
  { title: '預計扣款總金額', key: 'totalAmount' },
  { title: '扣款帳號', key: 'account' },
  { title: '聯絡信箱', key: 'email' },
  { title: '', key: 'editAction', sortable: false, width: '60px' }
]

const preferences = ref([])
const accountOptions = ref([])
const loading = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const valid = ref(false)

const editedItem = ref({ 
  sn: null, 
  userId: '', 
  productName: '',
  price: 0,
  feeRate: 0,
  purchaseQuantity: 1, 
  account: '' 
})

const onAccountSelected = (selectedAcc) => {
  const match = accountOptions.value.find(acc => acc.account === selectedAcc)
  if (match) {
    editedItem.value.userId = match.userId
  } else {
    editedItem.value.userId = ''
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const resAcc = await api.get('/users/active')
    if (resAcc.success) accountOptions.value = resAcc.data

    const resPref = await api.get('/preferences/active')
    if (resPref.success) {
      preferences.value = resPref.data.map(pref => {
        const found = accountOptions.value.find(u => u.userId === pref.userId)
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

const openDialog = (item = null) => {
  if (item) {
    isEdit.value = true
    let calculatedFeeRate = item.feeRate
    if (calculatedFeeRate === undefined) {
      if (item.price && item.purchaseQuantity) {
        calculatedFeeRate = item.totalFee / (item.price * item.purchaseQuantity)
        calculatedFeeRate = Number(calculatedFeeRate.toFixed(4))
      } else {
        calculatedFeeRate = 0
      }
    }
    editedItem.value = { ...item, feeRate: calculatedFeeRate }
  } else {
    isEdit.value = false
    editedItem.value = { 
      sn: null, 
      userId: '', 
      productName: '',
      price: 0,
      feeRate: 0,
      purchaseQuantity: 1, 
      account: '' 
    }
  }
  dialog.value = true
}

const closeDialog = () => {
  dialog.value = false
}

const save = async () => {
  try {
    let res
    if (isEdit.value) {
      res = await api.put(`/preferences/${editedItem.value.sn}`, editedItem.value)
    } else {
      res = await api.post('/preferences', editedItem.value)
    }
    if (res.success) {
      showSuccess('儲存成功')
      closeDialog()
      fetchData()
    } else {
      showError(res.message)
    }
  } catch (error) {
    showError(error)
  }
}

const deletePreference = async (sn) => {
  const result = await Swal.fire({
    title: '確認刪除',
    text: '確定要刪除此喜好清單嗎？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#1eacad',
    cancelButtonColor: '#1eacad',
    confirmButtonText: '確定',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    try {
      const res = await api.delete(`/preferences/${sn}`)
      if (res.success) {
        showSuccess('已刪除喜好清單')
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
