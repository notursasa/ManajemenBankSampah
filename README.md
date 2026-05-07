# 🌱 Manajemen Bank Sampah

Aplikasi **Manajemen Bank Sampah** adalah sebuah sistem desktop berbasis **JavaFX** yang dirancang untuk memudahkan pengelolaan bank sampah. Aplikasi ini memungkinkan pengguna untuk menyetorkan sampah, mendapatkan poin, dan menukarkan poin tersebut dengan berbagai hadiah menarik.

## 🌟 Fitur Utama

- **Autentikasi Pengguna**: Sistem Login dan Register dengan keamanan hashing password (SHA-256).
- **Setor Sampah**: Pengguna dapat mencatat penyetoran sampah berdasarkan kategori dan berat.
- **Sistem Poin**: Poin secara otomatis diakumulasi setelah setoran sampah diverifikasi.
- **Tukar Poin (Redeem Rewards)**: Poin yang terkumpul dapat ditukarkan dengan berbagai hadiah yang tersedia.
- **Riwayat Transaksi**: Pengguna dapat melihat riwayat penyetoran sampah dan penukaran poin.
- **Notifikasi Discord**: Terintegrasi dengan Discord Webhook untuk memberikan notifikasi otomatis ke server Discord.

## 🛠️ Teknologi yang Digunakan

- **Bahasa Pemrograman**: Java 11+
- **GUI Framework**: JavaFX
- **Database**: MariaDB
- **Build Tool**: Maven

## 📋 Persyaratan Sistem

Sebelum menjalankan aplikasi, pastikan Anda telah menginstal:

1. [Java Development Kit (JDK) 11 atau lebih baru](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
2. [MariaDB](https://mariadb.org/download/) atau XAMPP (dengan MySQL/MariaDB)
3. [Apache Maven](https://maven.apache.org/download.cgi) (opsional, jika menjalankan via CLI)
4. IDE (seperti NetBeans, IntelliJ IDEA, atau Eclipse)

## 🚀 Cara Instalasi & Menjalankan Aplikasi

### 1. Konfigurasi Database
1. Buka database client (seperti phpMyAdmin, DBeaver, atau HeidiSQL).
2. Buat database baru dengan nama `bank_sampah_db`.
3. Import tabel-tabel yang dibutuhkan (pastikan Anda memiliki struktur tabel untuk `users`, `waste_deposits`, `rewards`, dan `points_transactions`).
4. (Opsional) Jika Anda menggunakan username dan password database selain default (`root` dan kosong), sesuaikan pengaturan koneksi di dalam file `App.java`:
   ```java
   private static final String DB_URL = "jdbc:mariadb://localhost:3306/bank_sampah_db";
   private static final String DB_USER = "root";
   private static final String DB_PASS = ""; 
   ```

### 2. Menjalankan Aplikasi

**Melalui NetBeans / IDE Lainnya:**
- Buka project di IDE Anda.
- Jalankan Maven Build (`Clean and Build`).
- Run project (pastikan `com.mycompany.manajemenbanksampah.App` di-set sebagai Main Class).

**Melalui Terminal/Command Prompt:**
- Navigasi ke direktori project:
  ```bash
  cd ManajemenBankSampah
  ```
- Jalankan perintah maven berikut:
  ```bash
  mvn clean javafx:run
  ```

## 📸 Screenshots
*(Tambahkan screenshot antarmuka aplikasi di sini)*

## 🤝 Kontribusi
Jika Anda ingin berkontribusi, silakan buat *Pull Request* atau ajukan *Issue* di repositori ini.

---
*Dibuat untuk mempermudah pengelolaan sampah dan menciptakan lingkungan yang lebih bersih! 🌍*
