# Absensi MIN4 Batang Hari v1.0.103

Proyek Android WebView untuk membangun APK melalui GitHub Actions.

- applicationId: `id.min4batanghari.absensi` (dipertahankan)
- versionName: `1.0.102`
- versionCode: `1103`
- HTML aplikasi: `app/src/main/assets/index.html`
- Kamera dan galeri: ditangani oleh `MainActivity.java` melalui pemilih gambar Android.

## Build APK
1. Unggah isi folder proyek ini ke repositori GitHub.
2. Buka tab Actions dan jalankan workflow `Build Absensi MIN4 APK`.
3. Unduh artifact `Absensi-MIN4-v1.0.103-debug`.

## Catatan pembaruan
Build debug GitHub Actions dapat memakai debug signing key yang berbeda pada runner yang berbeda. APK ini belum dijamin dapat dipasang sebagai pembaruan di atas instalasi lama. Untuk pembaruan tanpa uninstall, diperlukan signing key tetap yang sama dengan APK lama. APK lama v1.0.98 tidak tersedia untuk mengambil sertifikatnya, sehingga jangan hapus aplikasi lama sebelum data dicadangkan dan strategi migrasi ditentukan.


Perbaikan v1.0.103: pemilih berkas Android mengikuti accept type dari HTML. JSON untuk pulihkan cadangan, CSV, dan DOCX dapat dipilih; pemilih kamera/galeri tetap khusus untuk gambar.

Perbaikan kamera v1.0.103: AndroidCameraBridge mengatur mode kamera/galeri; tombol Ambil dari Kamera membuka kamera langsung, tombol Galeri membuka galeri langsung.
