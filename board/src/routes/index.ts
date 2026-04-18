import { Router } from "express"
import notice from "./noticeRoutes"

const router = Router();

// router.use ('/경로' , 하위 라우터  )
router.use('/notice',notice)
// 이하 라우터 추가~~


export default router;