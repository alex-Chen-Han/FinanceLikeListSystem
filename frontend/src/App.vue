<template>
  <v-app>
    <v-app-bar color="primary" dark>
      <v-toolbar-title>金融商品喜好紀錄系統</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn flat to="/accounts">扣款帳戶管理</v-btn>
      <v-btn flat to="/preferences">喜好清單管理</v-btn>
      <v-menu>
        <template v-slot:activator="{ props }">
          <v-btn flat v-bind="props">
            最近刪除
            <v-icon right>mdi-menu-down</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item to="/accounts/deleted" exact>
            <v-list-item-title>已刪除帳戶</v-list-item-title>
          </v-list-item>
          <v-list-item to="/preferences/deleted" exact>
            <v-list-item-title>已刪除清單</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-app-bar>

    <v-main>
      <v-container>
        <router-view></router-view>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
</script>

<style>
</style>
