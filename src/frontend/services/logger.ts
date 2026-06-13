type LogLevel = 'info' | 'warn' | 'error' | 'debug';

class Logger {
  private formatMessage(level: LogLevel, message: string): string {
    return `[${new Date().toISOString()}] [${level.toUpperCase()}] ${message}`;
  }

  info(message: string, ...args: any[]) {
    console.info(this.formatMessage('info', message), ...args);
  }

  warn(message: string, ...args: any[]) {
    console.warn(this.formatMessage('warn', message), ...args);
  }

  error(message: string, ...args: any[]) {
    console.error(this.formatMessage('error', message), ...args);
  }

  debug(message: string, ...args: any[]) {
    if (import.meta.env.DEV) {
      console.debug(this.formatMessage('debug', message), ...args);
    }
  }
}

export const logger = new Logger();
