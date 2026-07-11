/**
 * Shared identity validation patterns for staff auth and account forms.
 * Keep in sync with backend BookingValidationPatterns and customer identityPatterns.
 */

export const FULL_NAME_REGEX = /^[\p{L}][\p{L}\s'.-]{1,99}$/u;
export const EMAIL_REGEX = /^[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}$/;

/** RFC 5321 maximum total length for an email address. */
export const EMAIL_MAX_LENGTH = 254;
export const FULL_NAME_MAX_LENGTH = 100;
export const USERNAME_MAX_LENGTH = 50;
export const PASSWORD_MAX_LENGTH = 72;

export const trimInput = (value) => (typeof value === 'string' ? value.trim() : value);
