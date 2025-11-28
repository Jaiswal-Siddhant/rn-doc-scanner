import { NitroModules } from 'react-native-nitro-modules';
import type { DocScanner } from './DocScanner.nitro';

const DocScannerHybridObject =
  NitroModules.createHybridObject<DocScanner>('DocScanner');

console.log('DocScannerHybridObject', DocScannerHybridObject);

export function multiply(a: number, b: number): number {
  return DocScannerHybridObject.multiply(a, b);
}

export async function scanDocument() {
  return await DocScannerHybridObject.scanDocument();
}
