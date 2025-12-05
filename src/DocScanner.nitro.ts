import type { HybridObject } from 'react-native-nitro-modules';

export enum ScannerMode {
  FULL = 1,
  BASE_WITH_FILTER = 2,
  BASE = 3,
}

export enum ResultFormat {
  JPEG = 101,
  PDF = 102,
}

/**
 * Configuration options for the document scanner.
 *
 * @interface ScanOptions
 * @property {boolean} [galleryImport]
 *        Allows selecting images from the device gallery. Defaults to `false`.
 *
 * @property {number} [pages]
 *        Maximum number of pages to scan. If omitted, a default (e.g., `1`) should be used. Defaults to `1`.
 *
 * @property {ScannerMode} [scannerMode]
 *        Defines the mode/behavior of the scanner (e.g., auto, manual). Defaults to `ScannerMode.BASE`.
 *
 * @property {ResultFormat} [resultFormat]
 *        Specifies the desired output format (e.g., file paths, base64). Defaults to `ResultFormat.JPEG`.
 */
export interface ScanOptions {
  galleryImport?: boolean;
  pages?: number;
  scannerMode?: ScannerMode;
  resultFormat?: ResultFormat;
}

export interface DocScanner
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  scanDocument(options?: ScanOptions): Promise<string[]>;
}
