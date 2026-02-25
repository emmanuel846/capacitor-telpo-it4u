import { registerPlugin } from '@capacitor/core';

import type { TpePrintPlugin } from './definitions';

const TpePrint = registerPlugin<TpePrintPlugin>('TpePrint', {
  web: () => import('./web').then(m => new m.TpePrintWeb()),
});

export * from './definitions';
export { TpePrint };
