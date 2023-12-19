# capacitor-telpo-it4u

capacitor plugin for telpo tpe print

## Install

```bash
npm install capacitor-telpo-it4u
npx cap sync
```

## API

<docgen-index>

* [`print(...)`](#print)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### print(...)

```typescript
print(options: { receipt: ReceiptModel; }) => Promise<void>
```

| Param         | Type                                                                |
| ------------- | ------------------------------------------------------------------- |
| **`options`** | <code>{ receipt: <a href="#receiptmodel">ReceiptModel</a>; }</code> |

--------------------


### Interfaces


#### ReceiptModel

| Prop         | Type                                                |
| ------------ | --------------------------------------------------- |
| **`title`**  | <code>string</code>                                 |
| **`footer`** | <code>string</code>                                 |
| **`lines`**  | <code>any</code>                                    |
| **`header`** | <code><a href="#headermodel">HeaderModel</a></code> |


#### HeaderModel

| Prop                | Type                |
| ------------------- | ------------------- |
| **`agencyName`**    | <code>string</code> |
| **`agencyContact`** | <code>string</code> |
| **`agencyAdress`**  | <code>string</code> |

</docgen-api>
