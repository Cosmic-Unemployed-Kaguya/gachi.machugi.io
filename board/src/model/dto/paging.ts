import z from "zod";


export const PagingReq = z.object({
    // query 는 무조건 string 이기에 숫자로 변환 필요 > .coerce.number()
    page : z.coerce.number().int().nonnegative(),   // 음수가 아닌 숫자
    size : z.coerce.number().int().positive().default(20),      // 양수
    search : z.string().max(20).nullish(),     // null,Undefined 가능  ,최대 20글자
    filter : z.string().max(20).nullish(),     // null Undefined 가능  ,최대 20글자
    sort : z.string().max(20).nullish(),       // null Undefined 가능  ,최대 20글자
})

export type PagingReqType = z.infer<typeof PagingReq>;

export interface Page<T> {
    items: T[];
    totalCount: number;  // 전체 데이터 수
    currentPage: number; // 현재 페이지
    totalPages: number;  // 전체 페이지 수
    hasNext: boolean;    // 다음 페이지 여부
}


// export const createPagingRes = <T extends z.ZodTypeAny>(itemSchema: T) => {
//     return z.object({
//         item : z.array(itemSchema),                             // 데이터 
//         totalCount : z.coerce.number().int().positive(),        // 양수, 데이터 총 개수
//         currentPage : z.coerce.number().int().positive(),       // 양수, 현재 페이지
//         totalPage : z.coerce.number().int().positive(),         // 양수, 최대 페이지
//         hasNext : z.boolean,                                    // boolean, 다음 페이지의 존재 여부
    
//     })
// }