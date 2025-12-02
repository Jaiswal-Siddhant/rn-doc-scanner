import type { HybridObject } from 'react-native-nitro-modules';

export interface ScanResult {
  pages?: Array<{ imageUri: string }>;
  pdf?: { uri: string; pageCount: number };
}

export interface ScanOptions {
  galleryImport?: boolean;
  pages?: number;
}

export interface DocScanner
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  scanDocument(options?: ScanOptions): Promise<string[]>;
}
