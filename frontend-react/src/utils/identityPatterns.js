/**
 * Shared identity validation patterns for auth, account, and booking forms.
 * Keep in sync with backend BookingValidationPatterns.
 */

export const FULL_NAME_REGEX = /^[\p{L}][\p{L}\s'.-]{1,99}$/u;
export const PHONE_REGEX = /^0(3|5|7|8|9)[0-9]{8}$/;
export const EMAIL_REGEX = /^[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}$/;

/** RFC 5321 maximum total length for an email address. */
export const EMAIL_MAX_LENGTH = 254;
export const FULL_NAME_MAX_LENGTH = 100;
export const PHONE_MAX_LENGTH = 10;

export const trimInput = (value) => (typeof value === 'string' ? value.trim() : value);

/** @deprecated Prefer named exports; kept for booking callers. */
export const BOOKING_VALIDATION = {
  FULL_NAME_REGEX,
  PHONE_REGEX,
  EMAIL_REGEX,
};
