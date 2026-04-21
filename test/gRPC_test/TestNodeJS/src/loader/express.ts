import express from 'express';
import routes from '../routes';

/** ({app} : {app: express.Application})
 * 특정 개체 app: express.Application 에서 app만 가져다 쓰겠다
 * app: express.Application  > express.Application에는 반드시 app이라는 키가 존재해야함!!
 */
export default ({app} : {app: express.Application}) => {

    app.get('/status', (req,res) => {
        res.status(200).end();
        });
    
    app.head('/status', (req,res) => {
        res.status(200).end();
        });

    // /routes 폴더 내 route들 추가
    app.use('/api', routes)

}