import { WebPlugin } from '@capacitor/core';

import type { ReceiptModel, TelpoPrintPlugin } from './definitions';

export class TelpoPrintWeb extends WebPlugin implements TelpoPrintPlugin {
  async print(options: { receipt: ReceiptModel }): Promise<void> {
    console.log('printing', options);
  }
}
