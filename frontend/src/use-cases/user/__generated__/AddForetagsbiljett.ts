/* tslint:disable */
/* eslint-disable */
// This file was automatically generated and should not be edited.

import { GiftCertificateDTOInput, GiftCertificateDTO_Status } from "./../../../__generated__/globalTypes";

// ====================================================
// GraphQL mutation operation: AddForetagsbiljett
// ====================================================

export interface AddForetagsbiljett_addGiftCertificates_giftCertificates {
  __typename: "GiftCertificateDTO";
  number: string;
  expiresAt: FilmstundLocalDate;
  status: GiftCertificateDTO_Status;
}

export interface AddForetagsbiljett_addGiftCertificates {
  __typename: "UserDTO";
  id: FilmstundUserID;
  giftCertificates: AddForetagsbiljett_addGiftCertificates_giftCertificates[];
}

export interface AddForetagsbiljett {
  addGiftCertificates: AddForetagsbiljett_addGiftCertificates;
}

export interface AddForetagsbiljettVariables {
  tickets?: GiftCertificateDTOInput[] | null;
}
