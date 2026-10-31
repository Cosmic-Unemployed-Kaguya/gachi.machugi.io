// quiz 서비스가 완성되기 전까지 game / room 연동 테스트를 위한 임시 gRPC 서버
// proto/machugi/quiz/quiz_internal.proto 의 QuizInternalService 3개 함수만 고정 데이터로 응답함
const path = require('path');
const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');

// 로컬 실행 시 레포 루트의 proto, 도커에서는 PROTO_ROOT 로 경로 지정
const PROTO_ROOT = process.env.PROTO_ROOT || path.resolve(__dirname, '../../proto');
const ADDRESS = process.env.GRPC_SERVER_ADDRESS || '0.0.0.0:9090';

const packageDefinition = protoLoader.loadSync('machugi/quiz/quiz_internal.proto', {
  includeDirs: [PROTO_ROOT],
  keepCase: true, // proto 필드명(snake_case) 그대로 사용
  longs: String, // uint64 는 문자열로 받음
  defaults: true,
});
const quizProto = grpc.loadPackageDefinition(packageDefinition).machugi.quiz;

// 고정 퀴즈 데이터 (GrpcQuizData 규격)
const QUIZZES = [
  { sort_order: 1, problem_text: '대한민국의 수도는?', problem_url: '', type: 'TEXT', answer: ['서울', '서울특별시'] },
  { sort_order: 2, problem_text: '1 + 1 = ?', problem_url: '', type: 'TEXT', answer: ['2', '둘'] },
  { sort_order: 3, problem_text: '사과를 영어로?', problem_url: '', type: 'TEXT', answer: ['apple'] },
];

const log = (method, req) => console.log(`[temp_quiz] ${method} ${JSON.stringify(req)}`);

const handlers = {
  // 퀴즈 존재 여부 : 0 이 아니면 항상 존재하는 것으로 응답
  IsQuiz(call, callback) {
    log('IsQuiz', call.request);
    callback(null, { is_quiz: call.request.quiz_idx !== '0' });
  },

  // 전체 퀴즈 : quiz_idx 와 상관없이 고정 데이터 전체 반환
  GetAllQuiz(call, callback) {
    log('GetAllQuiz', call.request);
    callback(null, { quizzes: QUIZZES });
  },

  // 일부 퀴즈 : 앞에서부터 count 개 반환 (0 이면 전체)
  GetPartQuiz(call, callback) {
    log('GetPartQuiz', call.request);
    const count = call.request.count || QUIZZES.length;
    callback(null, { quizzes: QUIZZES.slice(0, count) });
  },
};

const server = new grpc.Server();
server.addService(quizProto.QuizInternalService.service, handlers);

server.bindAsync(ADDRESS, grpc.ServerCredentials.createInsecure(), (err, port) => {
  if (err) {
    console.error('[temp_quiz] 서버 시작 실패 :', err);
    process.exit(1);
  }
  console.log(`[temp_quiz] QuizInternalService listening on ${ADDRESS} (port ${port})`);
});

// docker stop 시 바로 종료되도록
const shutdown = () => server.tryShutdown(() => process.exit(0));
process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
