import { Request } from "express";

import { UserRole } from "../model/enum/userRole";

// Request를 상속, 각 전송 방식에 대해 data를 넣을 수 있는 공간을 만들어준다
// + user에 대한 데이터 (user 확인이 필요없는 로직이 있기에 Optional)
// why? middleware 및 controller 끼리 데이터를 주고 받기 위함
export interface AppRequest extends Request {
  bodyData?: any;
  queryData?: any;
  paramsData?: any;
  userData?: UserData;
}

// userIdx는 헤더에 포함되어있기에 반드시 있다고 가정. 나머지는 Optional
export interface UserData {
  userIdx: number;
  userRole?: UserRole;
  userNickName?: string;
}
