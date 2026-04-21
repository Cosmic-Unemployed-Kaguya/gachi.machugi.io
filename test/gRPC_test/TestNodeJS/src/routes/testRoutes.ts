import { Router } from 'express';
import { testMiddle, testMiddle2,testMiddle3 } from '../middlewares/testMiddlewares';
import { testAPI, testAPI2, testAPI3, testAPI4 } from '../controller/testController';


const route = Router();

route
    .route('/')
    .get(testMiddle, testAPI)

// route   
//     .route('/connet/:name')
//     .get(testMiddle2, testAPI2)

route
    .route('/typeorm')
    .get(testMiddle3, testAPI3)

route
    .route('/gRPC')
    .get(testMiddle, testAPI4)

export default route;