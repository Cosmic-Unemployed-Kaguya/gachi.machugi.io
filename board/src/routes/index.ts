import { Router } from "express"
import notice from "./noticeRoutes"
import quiz from "./quizCommentRoutes"

const router = Router();

// router.use ('/경로' , 하위 라우터  )
// 이곳에서 notice, quizComment 등 하위 라우터들을 총합해서 외부 loader로 보내줄것임

router.use('/notice',notice)
router.use('/quiz', quiz)
// 이하 라우터 추가~~


export default router;