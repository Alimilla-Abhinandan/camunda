/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React, {useContext} from 'react';
import PropTypes from 'prop-types';

import {InstanceSelectionContext} from 'modules/contexts/InstanceSelectionContext';
import Button from 'modules/components/Button';
import pluralSuffix from 'modules/utils/pluralSuffix';

import Paginator from './Paginator';
import {getMaxPage} from './service';
import * as Styled from './styled';

function ListFooter({
  filterCount,
  perPage,
  firstElement,
  onFirstElementChange,
  hasContent
}) {
  const {getSelectedCount} = useContext(InstanceSelectionContext);
  const maxPage = getMaxPage(filterCount, perPage);

  const isPaginationRequired = (maxPage, total) => {
    return !(maxPage === 1 || total === 0);
  };

  return (
    <Styled.Footer>
      {hasContent && (
        <>
          <Styled.OperationButtonContainer>
            {getSelectedCount(filterCount) > 0 && (
              <Button color="primary" size="small">
                Apply Operation on{' '}
                {pluralSuffix(getSelectedCount(filterCount), 'Instance')}...
              </Button>
            )}
          </Styled.OperationButtonContainer>
          <div>
            {isPaginationRequired(maxPage, filterCount) ? (
              <Paginator
                firstElement={firstElement}
                perPage={perPage}
                maxPage={maxPage}
                onFirstElementChange={onFirstElementChange}
              />
            ) : null}
          </div>
        </>
      )}
      <Styled.Copyright />
    </Styled.Footer>
  );
}

ListFooter.propTypes = {
  onFirstElementChange: PropTypes.func.isRequired,
  perPage: PropTypes.number.isRequired,
  firstElement: PropTypes.number.isRequired,
  filterCount: PropTypes.number.isRequired,
  hasContent: PropTypes.bool.isRequired
};

export default ListFooter;
