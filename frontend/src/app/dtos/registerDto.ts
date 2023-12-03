export class RegisterDto {
  constructor(
    public email: string,
    public name: string,
    public passwordEncoded: string
  ) {}
}
