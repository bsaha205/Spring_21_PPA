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
inferTypes :: ConstraintEnv -> Exp -> (ConstraintEnv, Type)
inferTypes cenv (EVar var) = throw ToImplement
inferTypes cenv (ELambda v body) = throw ToImplement
inferTypes cenv (EApp fn arg) = throw ToImplement
inferTypes cenv (ECond pred tbody fbody) = throw ToImplement
inferTypes cenv (EPlus op1 op2) = throw ToImplement
inferTypes cenv (EPrim (PNum _)) = throw ToImplement
inferTypes cenv (EPrim (PBool _)) = throw ToImplement
inferTypes cenv (ELet s body) = throw ToImplement

{- Top-level type inference function. I will be calling it on Submitty. -}
inferType :: Exp -> Type
inferType exp = throw ToImplement

conc:: Ident -> Ident
conc x = concatenation x

