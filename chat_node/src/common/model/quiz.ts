import z from "zod";

export const QuizAnswerReq = z.object({
  sortOrder: z.number().int(),
  problemText: z.string().max(500),
  problemUrl: z.string().max(2048), 
  type: z.string(),
  answer: z.string().max(255),
});

export type QuizAnswerReq = z.infer<typeof QuizAnswerReq>;


export interface QuizData{
    sortOrder: number;      // 순서
    problemText: string;    // 문제 내용
    problemUrl: string;     // 문제 미디어 URL
    type: string;           // 문제 타입
}