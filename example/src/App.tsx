import { useEffect, useState } from 'react';
import { Text, View, StyleSheet } from 'react-native';
import { scanDocument } from 'react-native-doc-scanner';
import {
  ResultFormat,
  ScannerMode,
  type ScanOptions,
} from '../../src/DocScanner.nitro';

const options: ScanOptions = {
  pages: 2,
  galleryImport: false,
  scannerMode: ScannerMode.FULL,
  resultFormat: ResultFormat.PDF,
};

export default function App() {
  const [result, setResult] = useState<string>('');

  const scan = async () => {
    const data = await scanDocument(options);
    setResult(data.join('\n'));
    console.log({ data });
  };

  useEffect(() => {
    scan();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
