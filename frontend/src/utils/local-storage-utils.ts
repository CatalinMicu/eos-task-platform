export class LocalStorageUtils {
  static readonly tokenKey = 'TASKS_TOKEN';

  static setItem(key: string, value: string): void {
    localStorage.setItem(key, value);
  }

  static getItem(key: string): string | null {
    return localStorage.getItem(key);
  }

  static deleteItem(key: string): void {
    localStorage.removeItem(key);
  }
}
