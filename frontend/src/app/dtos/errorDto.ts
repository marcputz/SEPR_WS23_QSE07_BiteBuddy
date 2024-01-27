export class ErrorDto {
  constructor(
    public status: number,
    public statusText: string,
    public statusDescription: string,
    public reason: string | null
  ) {
  }
}
