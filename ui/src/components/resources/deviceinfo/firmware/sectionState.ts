export const SectionState = {
  Loading: "loading",
  Loaded: "loaded",
  Error: "error",
} as const;

export type SectionState = typeof SectionState[keyof typeof SectionState];

export type SectionStateChangeEvent = {
  state: SectionState;
  message?: string;
};
