# react-native-doc-scanner

<!-- ## 📄 Document Scanner (React Native) -->

A lightweight and fast **document scanning module** for React Native, built with Kotlin.  
It allows users to scan documents with high accuracy, crop, resize, apply filters, and export multi-page scans as images or PDF.

### ✨ Features
- 📷 Document scanning with edge detection  
- ✂️ Crop & resize controls  
- 🎨 Image filters (grayscale, enhance, threshold, etc.)  
- 📚 Multi-page scanning  
- 📤 Export as Image or PDF  

---

### ✅ Feature Tracker

- [x] Document Scan  
- [x] Android Support 
- [ ] iOS Support (WIP)  
- [ ] Add options for following
    - [ ] Crop Tool  
    - [ ] Resize  
    - [ ] Filters (In Progress)  
    - [ ] Multi-page Scan  
    - [ ] PDF Export  


## 📦 Installation


```sh
npm install react-native-doc-scanner react-native-nitro-modules

> `react-native-nitro-modules` is required as this library relies on Nitro Modules.
```


## 🚀 Usage


```js
import { scanDocument } from 'react-native-doc-scanner';

// ...
const result = await scanDocument();
```


## 🤝 Contributing
If you find any issue or have suggestions, please feel free to **open an issue** or **send a patch/PR**. Contributions are always welcome!

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT
