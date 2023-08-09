import { Notification } from 'element-ui';

class ErrorCode {
    static SUCCESS = 'SUCCESS';
}

export const SUCCESS = 'SUCCESS';

class GlobalHandledRespCode {
    codeHandlerMapping: any = {};
    constructor() {
        this.codeHandlerMapping = {
            TOKEN_MISSING: () => {
                Notification.error('OTHER ERROR');
            },
            SERVER_ERROR: () => {
                Notification.error('SERVER ERROR');
            },
        };
    }

    handle(code: string) {
        if (this.codeHandlerMapping[code] != null) {
            this.codeHandlerMapping[code]();
        } else {
            Notification.error(code);
        }
    }

    isGlobalHandled(code: string): boolean {
        return this.codeHandlerMapping[code] != null;
    }
}
export { ErrorCode };

export default new GlobalHandledRespCode();
