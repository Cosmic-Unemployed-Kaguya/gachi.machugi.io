import z from "zod";

// Req.Param
export const IdxParamReq = z.object({
    idx: z.coerce.number().int().positive(), 
})

export type IdxParamReqType = z.infer<typeof IdxParamReq>;