import { WebPlugin } from '@capacitor/core';

import type { ReceiptModel, TpePrintPlugin } from './definitions';

export class TpePrintWeb extends WebPlugin implements TpePrintPlugin {
  async print(options: { receipt: ReceiptModel }): Promise<void> {
    console.log('printing', options);
  }
}
