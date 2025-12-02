import { useEffect, useState } from 'react';
import { Text, View, StyleSheet } from 'react-native';
import { scanDocument } from 'react-native-doc-scanner';

export default function App() {
  const [result, setResult] = useState<string>('');

  const scan = async () => {
    const data = await scanDocument();
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
