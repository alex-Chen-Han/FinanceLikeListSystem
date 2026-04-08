USE FinanceLikeListSystem;
GO

-- =============================================
-- Users 預存程序
-- =============================================

CREATE OR ALTER PROCEDURE SP_InsertUser
    @UserId VARCHAR(11),
    @UserName NVARCHAR(50),
    @Email VARCHAR(100),
    @Account VARCHAR(50)
AS
BEGIN
    INSERT INTO Users (UserId, UserName, Email, Account, IsDeleted, CreatedAt, UpdatedAt)
    VALUES (@UserId, @UserName, @Email, @Account, 0, GETDATE(), GETDATE());
END;
GO

CREATE OR ALTER PROCEDURE SP_CheckUserExists
    @UserId VARCHAR(11),
    @Account VARCHAR(50),
    @Exists BIT OUTPUT
AS
BEGIN
    IF EXISTS (SELECT 1 FROM Users WHERE UserId = @UserId OR Account = @Account)
        SET @Exists = 1;
    ELSE
        SET @Exists = 0;
END;
GO

CREATE OR ALTER PROCEDURE SP_GetActiveUsers
AS 
BEGIN 
    SELECT * FROM Users WHERE IsDeleted = 0 ORDER BY CreatedAt DESC; 
END; 
GO

CREATE OR ALTER PROCEDURE SP_GetDeletedUsers
AS 
BEGIN 
    SELECT * FROM Users WHERE IsDeleted = 1 ORDER BY CreatedAt DESC; 
END; 
GO

CREATE OR ALTER PROCEDURE SP_GetUserById 
    @UserId VARCHAR(11)
AS 
BEGIN 
    SELECT * FROM Users WHERE UserId = @UserId; 
END; 
GO

CREATE OR ALTER PROCEDURE SP_UpdateUser
    @UserId VARCHAR(11), 
    @UserName NVARCHAR(50), 
    @Email VARCHAR(100), 
    @Account VARCHAR(50)
AS 
BEGIN
    UPDATE Users 
    SET UserName=@UserName, Email=@Email, Account=@Account, UpdatedAt=GETDATE() 
    WHERE UserId=@UserId AND IsDeleted=0;

    UPDATE LikeLists
    SET Account=@Account
    WHERE UserId=@UserId AND IsDeleted=0;
END; 
GO

CREATE OR ALTER PROCEDURE SP_DeleteUserSoft 
    @UserId VARCHAR(11)
AS 
BEGIN 
    UPDATE Users SET IsDeleted=1, UpdatedAt=GETDATE() WHERE UserId=@UserId; 
END; 
GO

CREATE OR ALTER PROCEDURE SP_DeleteUserHard
    @UserId VARCHAR(11)
AS 
BEGIN
    DELETE FROM Users WHERE UserId = @UserId;
END;
GO

CREATE OR ALTER PROCEDURE SP_RestoreUser
    @UserId VARCHAR(11)
AS 
BEGIN
    UPDATE Users SET IsDeleted = 0, UpdatedAt = GETDATE() WHERE UserId = @UserId;
END;
GO

-- =============================================
-- LikeLists & Products 預存程序 
-- =============================================

CREATE OR ALTER PROCEDURE SP_InsertProduct
    @ProductName NVARCHAR(100), 
    @Price DECIMAL(18,4), 
    @FeeRate DECIMAL(18,4),
    @No INT OUTPUT
AS 
BEGIN
    INSERT INTO Products (ProductName, Price, FeeRate, CreatedAt)
    VALUES (@ProductName, @Price, @FeeRate, GETDATE());
    
    SET @No = SCOPE_IDENTITY();
END; 
GO

CREATE OR ALTER PROCEDURE SP_GetAllProducts 
AS 
BEGIN 
    SELECT * FROM Products ORDER BY CreatedAt DESC; 
END; 
GO

CREATE OR ALTER PROCEDURE SP_GetProductByNo 
    @No INT 
AS 
BEGIN 
    SELECT * FROM Products WHERE No = @No; 
END; 
GO

CREATE OR ALTER PROCEDURE SP_InsertLikeList
    @UserId VARCHAR(11), 
    @ProductNo INT, 
    @PurchaseQuantity INT, 
    @Account VARCHAR(50),
    @TotalFee DECIMAL(18,4), 
    @TotalAmount DECIMAL(18,4), 
    @SN INT OUTPUT
AS 
BEGIN
    INSERT INTO LikeLists (UserId, ProductNo, PurchaseQuantity, Account, TotalFee, TotalAmount, IsDeleted, CreatedAt)
    VALUES (@UserId, @ProductNo, @PurchaseQuantity, @Account, @TotalFee, @TotalAmount, 0, GETDATE());
    SET @SN = SCOPE_IDENTITY();
END; 
GO

CREATE OR ALTER PROCEDURE SP_GetActiveLikeLists 
AS 
BEGIN
    SELECT L.*, U.UserName, U.Email, P.ProductName, P.Price, P.FeeRate
    FROM LikeLists L 
    INNER JOIN Users U ON L.UserId=U.UserId 
    INNER JOIN Products P ON L.ProductNo=P.No
    WHERE L.IsDeleted = 0 
    ORDER BY L.CreatedAt DESC;
END; 
GO

CREATE OR ALTER PROCEDURE SP_GetDeletedLikeLists 
AS 
BEGIN
    SELECT L.*, U.UserName, U.Email, P.ProductName, P.Price, P.FeeRate
    FROM LikeLists L 
    INNER JOIN Users U ON L.UserId=U.UserId 
    INNER JOIN Products P ON L.ProductNo=P.No
    WHERE L.IsDeleted = 1 
    ORDER BY L.CreatedAt DESC;
END; 
GO

CREATE OR ALTER PROCEDURE SP_UpdateLikeList
    @SN INT, 
    @UserId VARCHAR(11),
    @Account VARCHAR(50),
    @ProductNo INT, 
    @PurchaseQuantity INT, 
    @TotalFee DECIMAL(18,4), 
    @TotalAmount DECIMAL(18,4)
AS 
BEGIN
    UPDATE LikeLists 
    SET UserId=@UserId, Account=@Account, ProductNo=@ProductNo, PurchaseQuantity=@PurchaseQuantity, TotalFee=@TotalFee, TotalAmount=@TotalAmount
    WHERE SN=@SN AND IsDeleted=0;
END; 
GO

CREATE OR ALTER PROCEDURE SP_DeleteLikeListSoft 
    @SN INT 
AS 
BEGIN 
    UPDATE LikeLists SET IsDeleted=1 WHERE SN=@SN; 
END; 
GO

CREATE OR ALTER PROCEDURE SP_RestoreLikeList 
    @SN INT 
AS 
BEGIN 
    UPDATE LikeLists SET IsDeleted=0 WHERE SN=@SN AND IsDeleted=1; 
END; 
GO

CREATE OR ALTER PROCEDURE SP_CheckUserHasLikeLists
    @UserId VARCHAR(11),
    @Count INT OUTPUT
AS 
BEGIN
    SELECT @Count = COUNT(*) FROM LikeLists WHERE UserId = @UserId;
END;
GO

CREATE OR ALTER PROCEDURE SP_DeleteLikeListHard
    @SN INT
AS 
BEGIN
    SET NOCOUNT ON;
    DECLARE @TargetProductNo INT;

    SELECT @TargetProductNo = ProductNo FROM LikeLists WHERE SN = @SN;

    BEGIN TRANSACTION;
    BEGIN TRY
        IF @TargetProductNo IS NULL
        BEGIN
            PRINT '找不到該序號的資料，不執行刪除。';
        END
        ELSE
        BEGIN
            DELETE FROM LikeLists WHERE SN = @SN;
            DELETE FROM Products WHERE No = @TargetProductNo;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW; 
    END CATCH
END;
GO