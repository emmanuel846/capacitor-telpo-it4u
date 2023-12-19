export interface TelpoPrintPlugin {
  print(options: { receipt: ReceiptModel }): Promise<void>;
}

export interface ReceiptModel {
    title: string;
    footer: string;
    lines: any;
    header: HeaderModel;
}
export interface HeaderModel{
    agencyName: string;
    agencyContact: string;
    agencyAdress: string;
}