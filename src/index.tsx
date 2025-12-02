import { NitroModules } from 'react-native-nitro-modules';
import type { DocScanner, ScanOptions } from './DocScanner.nitro';

const DocScannerHybridObject =
  NitroModules.createHybridObject<DocScanner>('DocScanner');

console.log('DocScannerHybridObject', DocScannerHybridObject);

export async function scanDocument(options?: ScanOptions) {
  return await DocScannerHybridObject.scanDocument(options);
}
