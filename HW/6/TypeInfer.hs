{-
  You will be filling in the gaps to implement simple type inference.  Please
  provide an implementation for all functions that raise the "ToImplement"
  exception.

  Feel free to add additional functions as you wish.
 -}
module TypeInfer (inferType) where
import Control.Exception
import Data
import Utils

unify :: Constraints -> Subst
unify = throw ToImplement


{- Performs type inference. -}
{--inferTypes :: ConstraintEnv -> Exp -> Type--}
inferTypes :: ConstraintEnv -> Exp -> (ConstraintEnv,Type)
{- Return the type of var by lookup in gamma -}
inferTypes cenv (EVar var) = let val = lookup var (tenv cenv)
                             in
                             case val of
                             Just v -> (cenv, v)
                             Nothing -> throw (UnboundVar var)

inferTypes cenv (ELambda v body) = let varV = lookup v (tenv cenv)
{--let varV = snd (inferTypes cenv (EVar v))--}
                                   in
                                   case varV  of
                                   Just va -> let e1 = inferTypes cenv body
                                              in 
                                              ((fst e1),(TArrow (va) (snd e1)))
                                 {--  TVar va -> let e1 = inferTypes cenv body
                                              in 
                                              ((fst e1),(TArrow (TVar va) (snd e1)))--}
                                   Nothing -> let type_cenv = addTEnv cenv v
                                                         in
											             let e1 = inferTypes (snd type_cenv) body
                                                         in 
                                                         ((fst e1),(TArrow (fst type_cenv) (snd e1)))
                                  

inferTypes cenv (EApp fn arg) = let e1 = inferTypes cenv fn
                                in
                                let e2 = inferTypes (fst e1) arg
                                in
                                let e3 = newTVar (fst e2)
                                in
                                let cenv_new = addConstraint (snd e3) (snd e1) (TArrow (snd e2) (fst e3))
                                in
                                (cenv_new, fst e3)

inferTypes cenv (ECond pred tbody fbody) = let e1 = inferTypes cenv pred
                                           in
                                           let cenv_1 = addConstraint cenv (snd e1) (boolType)
                                           in
                                           let e2 = inferTypes cenv_1 tbody
                                           in
                                           let e3 = inferTypes cenv_1 fbody
                                           in
                                           let cenv_2  = addConstraint cenv_1 (snd e2) (snd e3)
                                           in
                                           (cenv_2, snd e2)

inferTypes cenv (EPlus op1 op2) = let e1 = inferTypes cenv op1
                                  in
                                  let cenv_1 = addConstraint (fst e1) (snd e1) (intType)
                                  in
                                  let e2 = inferTypes cenv_1 op2
                                  in
                                  let cenv_2 = addConstraint (fst e2) (snd e2) (intType)
                                  in
                                  (cenv_2, intType)


inferTypes cenv (EPrim (PNum _)) = (cenv, intType)

inferTypes cenv (EPrim (PBool _)) = (cenv, boolType)

inferTypes cenv (ELet s body) = throw ToImplement

{- Top-level type inference function. I will be calling it on Submitty. -}
inferType :: Exp -> Type
inferType exp = let t = inferTypes (CEnv{constraints=[], var = 0, tenv = []}) exp
                in
				let s = unifySet (constraints (fst t))
				in
                applySubst (snd t) s