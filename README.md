# 金融商品喜好紀錄系統 (Finance LikeList System)

## 專案名稱與簡介
這是一個金融商品喜好紀錄系統，提供用戶管理帳戶與喜好清單等相關功能。其中支援資料的「軟刪除」與「永久刪除」及還原，介面採用玉山銀行風格配色，提供金融領域使用者直覺且安全的操作體驗。

---

## 核心功能
1. **扣款帳戶管理 (User Accounts)**
   - 包含會員 ID、姓名、Email、預設扣款帳號的管理。
   - 提供重複使用者的前端與後端雙重即時檢查。
   - 若帳戶下已有綁定喜好清單，系統會以防呆機制阻止刪除。

2. **金融商品喜好清單 (Preference Lists)**
   - 記錄使用者的金融商品需求，並包含單價、購買數量、手續費率與預計扣款總金額等紀錄。
   - 新增或修改時系統將自動計算「總手續費」與「預計扣款總金額」，計算公式為總金額 = ⌊(單價 × 數量) + 手續費⌋，系統將針對最終試算結果進行無條件捨去至整數。

3. **資源回收筒機制 (Soft / Hard Delete)**
   - 資料預設刪除行為皆為「軟刪除 (Soft Delete)」，被刪除之帳戶與喜好清單可於下拉式的「最近刪除」檢視中進行**還原**。
   - 提供更進階的**永久刪除 (Hard Delete)**，確保有完全抹除底層資料的功能。

---

## 技術架構
- **前端**: `Vue 3 (Vite), Axios, SweetAlert2`
- **後端**: `Java, Spring Boot`
- **資料庫**: `Microsoft SQL Server` (MSSQL)
- **管理工具**: `Maven, NPM`

> **分層設計說明 (Spring Boot 架構)**：
> - `Controller` (展示層介接)：封裝 RESTful API 以及通用回傳格式 `ApiResponse`。
> - `Service` (業務層)：負責核心商業邏輯、手續費計算、阻斷邏輯、交易控制並封裝各層例外處理。
> - `Repository` (資料層)：以 Spring JDBC 與 `SimpleJdbcCall` 對接 SQL 預存程序 (Stored Procedures)。

---

## 技術重點
1. **防範 SQL Injection 以及 XSS 攻擊**：
   - **防範 SQL Injection**：資料存取層不拼接來源字串，而是全數仰賴 **Spring JDBC Parameterized Queries (參數化查詢)** 搭配 SQL Server 本地的 **Stored Procedure (預存程序)**，強制將輸入值視為參數而非可執行語法，徹底阻斷隱碼攻擊的可能性。
   - **防範 XSS**：後端實作自主 `XssFilter` 和 `XssRequestWrapper` 在請求初期提早濾除並跳脫惡意 HTML/JavaScript 標籤，配合前端 Vue.js 樣板特性，前後端雙重防護。

2. **實作 Transaction，避免同時異動多個資料表時造成資料錯亂**：
   - 當進行新增或編輯喜好清單操作時，底層牽涉到雙表異動（依序寫入新實體至 `Products` 表以取得流水號，隨後寫入 `LikeLists`）。
   - 後端在 `LikeListService` 中運用了 `@Transactional` 註解來確保整個業務邏輯包裹在同一個交易週期。只要發生任何例外錯誤，操作就會全數 **Rollback（還原）**，確保資料一致性。

---

## 資料庫邏輯說明
1. **透過 Stored Procedure 存取資料庫**
   所有的CRUD行為皆撰寫於 SQL 預存程序內，後端 Java 原碼內完全不留存原生 SQL 語法，確保資料存取邏輯皆在 DB 端進行。
2. **防呆驗證依賴 (Validation)**
   在決定是否「刪除帳戶」時，系統會調用 `SP_CheckUserHasLikeLists` 先確認是否存有綁定依賴，有依賴則立即擲回 Exception 給前端。

---

## 檔案結構 (概要)
```text
FinanceLikeListSystem/
├── DB/                      // 資料庫定義與初始化腳本 (DDL/DML)
├── backend/                 // Spring Boot 後端專案
└── frontend/                // Vue 3 前端專案
```

---

## 環境版本檢查
為保證系統正確編譯與執行，請確保您的開發環境安裝以下主流元件標準版本：
*   **Java**: JDK 17 (或以上)
*   **Node.js**: v18.0+ 
*   **npm**: v9.0+
*   **資料庫**: Microsoft SQL Server (搭配其對應的 SSMS 操作介面)
*   **建置管理**: Apache Maven (Spring Boot 依存)

---

## 資料庫帳號密碼確認
要讓本機後端專案與 MSSQL 順利連線，請檢查/更新後端資源夾內的配置：
*   **檔案路徑**: `\backend\src\main\resources\application.properties`
*   **重點修改行數**:
    ```properties
    spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=FinanceLikeListSystem;encrypt=false;trustServerCertificate=true;
    spring.datasource.username=owner   # <== 確認此處 MSSQL 使用者帳號，請自行修改或新增帳號
    spring.datasource.password=12345   # <== 確認此處對應的資料庫密碼，請自行修改或新增密碼
    ```

---

## 如何啟動執行

### 1. 建立資料庫 (首要步驟)
請於SQL Server 介面內依序執行 `\DB\` 中的 `DDL(Schema).sql`、`DDL(Stored_Procedure).sql`、`DML(Data).sql` 指令碼，以完成建表與預存程序的佈署。

### 2. 啟動後端服務 (Spring Boot)
請執行[FinanceLikeListApplication.java](backend/src/main/java/com/finance/likelist/FinanceLikeListApplication.java)，啟動後端系統。

### 3. 啟動前端服務 (Vue 3 UI)
另開一個新的終端機，並切換路徑至 `frontend/`：
```bash
# 下載套件相關依賴 (初次使用)
npm install

# 啟動開發伺服器
npm run dev
```
啟動完畢後，開啟瀏覽器造訪終端機所提示的網址 (通常為 `http://localhost:5173`)，即可開始體驗使用金融商品喜好紀錄系統！
