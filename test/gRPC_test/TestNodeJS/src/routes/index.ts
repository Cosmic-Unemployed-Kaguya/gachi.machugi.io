import { Router } from "express"
import test from "./testRoutes"

const router = Router();

// router.use ('/경로' , 하위 라우터  )
router.use('/test',test)
// 이하 라우터 추가~~


export default router;