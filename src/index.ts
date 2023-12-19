import { registerPlugin } from '@capacitor/core';

import type { TelpoPrintPlugin } from './definitions';

const TelpoPrint = registerPlugin<TelpoPrintPlugin>('TelpoPrint', {
  web: () => import('./web').then(m => new m.TelpoPrintWeb()),
});

export * from './definitions';
export { TelpoPrint };
