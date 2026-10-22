export type Messages = Record<string, Record<string, unknown>>;

/** Hook for consumers: shadow this file to add or override i18n messages. */
export default function customizeMessages(messages: Messages): Messages {
    return messages;
}
