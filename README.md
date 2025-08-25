# FlickrProject

A sample Android app built with **Jetpack Compose**, **Hilt**, **Retrofit**, and **Coil** that integrates with the [Flickr API](https://www.flickr.com/services/api/).  
Users can search photos, scroll infinitely through results, and view photo details with pinch-to-zoom support.

---

## ✨ Features

- 🔍 **Search photos** by keyword (powered by Flickr search API).  
- 📜 **Infinite scrolling** for search results.  
- 🖼️ **Photo details screen** with metadata and hero image.  
- 👆 **Pinch-to-zoom** and double-tap zoom on detail photos.  
- 💉 **Hilt DI** for dependency injection.  
- 🌐 **Retrofit + Moshi** for networking and JSON parsing.  
- 🖼️ **Coil** for efficient image loading and caching.  
- 🧪 **Testing** with `MockWebServer` and **Compose UI tests** (E2E flow: search → scroll → open detail).

---

## 🛠️ Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose + Material 3  
- **DI:** Hilt  
- **Networking:** Retrofit + OkHttp + Moshi  
- **Image Loading:** Coil  
- **Testing:** JUnit4, MockWebServer, Compose UI Test  
- **Architecture:** MVVM with `ViewModel` + `StateFlow`

---

## 📸 Screenshots
<img width="270" height="600" alt="Screenshot_20250825-112356" src="https://github.com/user-attachments/assets/abb53e2a-2561-43ea-b81d-968e2475f458" />
<img width="270" height="600" alt="Screenshot_20250825-112409" src="https://github.com/user-attachments/assets/e0b2b368-613b-4686-8fe0-56f10c0a5cbc" />

---

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/yourusername/FlickrProject.git
   cd FlickrProject
