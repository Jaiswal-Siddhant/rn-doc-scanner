import { useEffect } from 'react';
import { Text, View, StyleSheet } from 'react-native';
import * as Scanner from 'react-native-doc-scanner';

const result = Scanner.multiply(3, 7);

export default function App() {
  const scanDocument = async () => {
    const data = await Scanner.scanDocument();
    console.log({ data });
  };

  useEffect(() => {
    scanDocument();
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
