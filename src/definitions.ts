export interface TpePrintPlugin {
  print(options: { receipt: ReceiptModel }): Promise<void>;
}

export interface ReceiptModel {
    title: string;
    footer: string;
    lines: any;
    header: HeaderModel;
    /** Encoded beneficiary payload used by Feitian native QR printing. */
    qrData?: string;
    /** PNG image without the data URL prefix for Telpo/Sunyard bitmap printing. */
    qrImageBase64?: string;
    qrLabel?: string;
}
export interface HeaderModel{
    agencyName: string;
    agencyContact: string;
    agencyAdress: string;
}