import type { HybridObject } from 'react-native-nitro-modules';

export interface ScanResult {
  pages?: Array<{ imageUri: string }>;
  pdf?: { uri: string; pageCount: number };
}

export interface DocScanner
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  multiply(a: number, b: number): number;
  scanDocument(): void;
}
